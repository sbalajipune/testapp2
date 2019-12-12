// openshift-utils methods

def createProject(project, jenkinsProject, imageStreamProject) {
    try {
        // try to create the project
        echo "Creating project ${project} if it does not exist yet"
        openshift.newProject(project, "--display-name", project)
        echo "Project ${project} has been created"
    } catch (e) {
        echo "${e}"
        echo "Check error.. but it could be that the project already exists... skkiping step"
    }
    // TODO To be decided.. => if the project was not created by jenkins sa, 
    //      then, it is vey likely that its sa doesnt have admin or edit role. If it was created by jenkins, jenkins sa will have admin role
    // openshift.policy("add-role-to-user", "edit", "system:serviceaccount:${jenkinsProject}:jenkins", "-n", project)
    // if project and imageStreamProject are different, add system:image-puller role to project sa
    if (!project.equals(imageStreamProject)) {
        openshift.policy("add-role-to-group", "system:image-puller", "system:serviceaccounts:${project}", "-n", imageStreamProject)
    }
}


def applyTemplate(project, templateFile, appName, appVersion, imageStreamProject, imageStreamName, customParameters, skipObjects) {
    echo "Applying template ${templateFile} in project ${project}. Application: ${appName}-${appVersion}"
    openshift.withProject(project) {
        def models = null;
        if (customParameters.startsWith("-p")) {
            echo "Additional parameters for template are ${customParameters}"
            models = openshift.process(readFile(file: templateFile), "-p NAME=${appName}", "-p APP_VERSION=${appVersion}", "-p IMAGESTREAM_PROJECT=${imageStreamProject}", "-p IMAGESTREAM_NAME=${imageStreamName}", "-p IMAGESTREAM_TAG=${appVersion}", customParameters)
        } else {
            models = openshift.process(readFile(file: templateFile), "-p NAME=${appName}", "-p APP_VERSION=${appVersion}", "-p IMAGESTREAM_PROJECT=${imageStreamProject}", "-p IMAGESTREAM_NAME=${imageStreamName}", "-p IMAGESTREAM_TAG=${appVersion}", "")
        }
        echo "Discarding objects of type ${skipObjects}"
        for (o in models) {
            // we will discard skipObjects
            def skip = false
            for (skipObject in skipObjects) {
                if (o.kind == skipObject) {
                    skip = true
                    break
                }
            }
            if (!skip) {
                echo "Applying changes on ${o.kind}"
                filterObject(o, project)
                def created = openshift.apply(o)
                // do we want to show "created"?
            }
        }
    }
}


def startBuildFromFile(project, appName, file, watchUntilCompletion) {
    echo "Starting binary build in project ${project} for application ${appName}"
    openshift.withProject(project) {
        echo "Using file ${file} in build"
        // start build
        def build = openshift.startBuild(appName, "--from-file=${file}")
        build.describe()
        if (watchUntilCompletion) {
            echo "user has requested to wait until build has finished"
            build.watch {
                return it.object().status.phase == "Complete"
            }
        }
    }
}


def deploy(project, appName) {
    echo "Deploying application ${appName} in project ${project}"
    openshift.withProject(project) {
        def dc = openshift.selector("dc", appName)

        def replicas = dc.object().spec.replicas
        def currentPods = dc.related('pods').count()

        def rm = dc.rollout()
        def lastDeploy = rm.latest()
        echo "${lastDeploy.out}"

        dc.related('pods').watch {
            // End the watch only when rolling new pods
            echo "Total number of current pods are ${it.count()} while old ones were ${currentPods}"
            return it.count() > currentPods
        }
        echo "Rolling out deployment"
        dc.related('pods').watch {
            // End the watch only once the exact number of replicas is back
            echo "New pods are ${it.count()} and should match ${replicas}"
            return it.count() == replicas
        }
        // Let's wait until pods are Running
        dc.related('pods').untilEach {
            echo "Pod ${it.object().metadata.name} is ${it.object().status.phase}"
            return it.object().status.phase == 'Running'
        }
        echo "New deployment ready"
    }
}


def filterObject(object, project) {
    // TODO extend with any possible rule for any object you want
    if (object.kind == "DeploymentConfig") {
        filterDeploymentConfig(object, project)
    }
}


def filterDeploymentConfig(dc, project) {
    echo "Filtering DeploymentConfig ${dc.metadata.name}"
    def currentDc = openshift.selector("dc", dc.metadata.name)
    if (currentDc.exists()) {
        def currentDcObject = currentDc.object()
        // reset annotation kubectl.kubernetes.io/last-applied-configuration
        sh "oc project ${project}"
        sh "oc annotate dc ${dc.metadata.name} kubectl.kubernetes.io/last-applied-configuration-"
        // save current replica number
        echo "Keeping replica number to ${currentDcObject.spec.replicas}"
        dc.spec.replicas = currentDcObject.spec.replicas
    }
}

def addVersionLabels(project, objectType, appName, appVersion) {
    openshift.withProject(project) {
        def p = openshift.selector(objectType, appName)
        if (p.exists()) {
            def object = p.object()
            def currentVersion = object.metadata.labels[appName + '-current-version']
            if (currentVersion != null) {
                object.metadata.labels[appName + '-rollback-version'] = currentVersion
            }
            object.metadata.labels[appName + '-current-version'] = appVersion
            openshift.apply(object)
        }

    }
}

def rollback(project, appName, imageStreamProject) {
    def succeeded = false

    openshift.withProject(project) {

        def p = openshift.selector("dc", appName)

        if (p != null && p.exists()) {

            def currentVersion = p.object().metadata.labels[appName + '-current-version']
            echo "Current version is ${currentVersion}"
            def rollbackVersion = p.object().metadata.labels[appName + '-rollback-version']
            echo "Rollback version is ${rollbackVersion}"

            if (currentVersion != null) {
                if (rollbackVersion != null) {
                    if (currentVersion != rollbackVersion) {

                        def dcObject = p.object()
                        // get tag used by the application (it is stored under version label) 
                        def tag = dcObject.metadata.labels['version']
                        openshift.tag("${imageStreamProject}/${appName}:${rollbackVersion}", "${imageStreamProject}/${appName}:${tag}")
                        dcObject.metadata.labels[appName + '-current-version'] = rollbackVersion
                        dcObject.metadata.labels[appName + '-rollback-version'] = rollbackVersion
                        openshift.apply(dcObject)
                        openshiftUtils.deploy(project, "${appName}")
			echo "Rollback succeeded"
                        succeeded = true

                    } else {
                        echo "Rollback could not be performed. Current and rollback labels are the same"
                    }
                } else {
                    echo "Rollback could not be performed. DeploymentConfig for application ${appName} is missing labels ${appName}-rollback-version"
                }
            } else {
                echo "Rollback could not be performed. DeploymentConfig for application ${appName} is missing labels ${appName}-current-version"
            }
        } else {
            echo "Application ${appName} does not exist in project ${project}"
        }
    }
    return succeeded
}

def addAlternateService(project, appName, alternateServiceName, weight) {
    sh "oc project ${project}"
    sh "oc patch route/${appName} -p '{\"spec\": { \"alternateBackends\": [ { \"kind\": \"Service\", \"name\": \"${alternateServiceName}\", \"weight\": ${weight} } ] } }'"
}

def resetAlternateService(project, appName) {
    sh "oc project ${project}"
    sh "oc patch route/${appName} -p '{\"spec\": { \"alternateBackends\": [ ] } }'"
}




return this
