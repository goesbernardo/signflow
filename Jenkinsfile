pipeline {
    agent any

    environment {
        APP_NAME = 'meu-app'
        IMAGE_NAME = "meu-app:${env.BUILD_NUMBER}"
        CONTAINER_NAME = 'meu-app-container'
        APP_PORT = '8081'        // porta da sua aplicação
        HOST_PORT = '81'         // porta exposta no EC2
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE_NAME} .
                    docker tag ${IMAGE_NAME} ${APP_NAME}:latest
                """
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sh """
                    # Para o container antigo se existir
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    # Sobe o novo container
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${HOST_PORT}:${APP_PORT} \
                        ${APP_NAME}:latest

                    # Limpa imagens antigas
                    docker image prune -f
                """
            }
        }
    }

    post {
        success { echo '✅ Deploy realizado com sucesso!' }
        failure { echo '❌ Falha no pipeline!' }
        always { cleanWs() }
    }
}