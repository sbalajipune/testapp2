# App Functionality
Society app that returns the details of apartments in a society
   e.g given the apartment id, return 
   1. the details of members in apartment
   2. the details of parking(s)
   3. Vehicle details owned by members of this apartment

This repo contains 4 springboot apps as below
1. member-service - returns the details of members in society e.g. member id, name, age, profession
2. vehicles-service - returns the details of vehicles registered in society e.g. registration id, owner member id, owner details (gets these details by calling member-setvice)
3. parking-service - returns the details of parkings in society e.g. parking id, apartment id, vehicle(s) details (gets these details by callling vehicles-service), owner details  (gets these details by calling member-setvice)
4. apartment-service - returns the details of apartments in society e.g. apartment id, owner details, members details (gets these details by calling member-setvice), parking details ((gets these details by calling parking-setvice)

# steps to run this app
1. clone this repo
   git clone <repo clone URL>

2. create the objects for this app in minishift/openshift cluster
   sh societyapp_create_objects.sh
   
   This will have created following
   1. postgresql - running service
   2. Jenkins - running service
   3. Nexus - running service
   4. BuildConfig, DeploymentConfig, Route and Service objects for following apps
		1. member-services
		2. vehicles-service
		3. parking-service
		4. apartment-service
   5. Jenkins pipelines for building and deploying above apps
        1. member-app-pipeline
		2. vehicles-app-pipeline
		3. parking-app-pipeline
		4. apartment-app-pipeline

3.  Start the pipelines to build and deploy apps
	sh societyapp_run_pipelines.sh
