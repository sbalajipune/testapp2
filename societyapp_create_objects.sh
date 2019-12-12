#!/bin/sh
echo "Creating objects, resources and pipelines for society-dec project"
oc new-project societyapp-dev
oc new-app sonatype/nexus
oc expose svc/nexus
sleep 10
oc new-app --docker-image=openshiftdemos/sonarqube:6.5
oc expose svc/sonarqube
sleep 10
oc new-app -f societyapp-dev.yaml
#echo "Creating deployment objects and postegre db instance for society-test project"
#oc new-project societyapp-test
#oc new-app -f societyapp-test.yaml
