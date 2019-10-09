PROJECT_NAME := TestData
DOCKER_NAME := my-java-app
EXTEND_FILE_NAME := DOCKER_
CSV_FILE_NAME_TESTOUT := TestData_out.csv
CSV_FILE_NAME_CALCULATIONS := test_data_out.csv

start-calc-and-test:
	 docker run -it --rm --name ${PROJECT_NAME} ${DOCKER_NAME}
	 docker cp ${PROJECT_NAME}:/myapp/TestData_out.csv .

build:
	docker build -t ${DOCKER_NAME} .

build-no-cache:
	docker build -t ${DOCKER_NAME} . --no-cache

drop:
	docker rm -f ${PROJECT_NAME}

take-out-files:
	docker cp ${PROJECT_NAME}:/myapp/${CSV_FILE_NAME_TESTOUT} ${EXTEND_FILE_NAME}${CSV_FILE_NAME_TESTOUT}
	docker cp ${PROJECT_NAME}:/myapp/${CSV_FILE_NAME_CALCULATIONS} ${EXTEND_FILE_NAME}${CSV_FILE_NAME_CALCULATIONS}
