PROJECT_NAME := TestData
DOCKER_NAME := my-java-app

up:
	 docker run -it --rm --name ${PROJECT_NAME} ${DOCKER_NAME}

build:
	docker build -t ${DOCKER_NAME} .

build-no-cache:
	docker build -t ${DOCKER_NAME} . --no-cache
