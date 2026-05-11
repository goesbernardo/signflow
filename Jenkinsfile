cat > ~/signflow/Jenkinsfile << 'EOF'
pipeline {
    agent any

    environment {
        APP_NAME = 'signflow-app'
        IMAGE_NAME = "signflow-app:${env.BUILD_NUMBER}"
        CONTAINER_NAME = 'signflow-app'
        APP_PORT = '8081'
        HOST_PORT = '8081'
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
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        --network signflow_default \
                        -p ${HOST_PORT}:${APP_PORT} \
                        -e CLICKSIGN_URL=https://sandbox.clicksign.com \
                        -e CLICKSIGN_API_TOKEN=43e5624f-84fa-4c72-b3c7-03202c34f8c6 \
                        -e CLICKSIGN_WEBHOOK_SECRET=webhook-secret \
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://signflow-db:5432/signflow \
                        -e SPRING_PROFILES_ACTIVE=prod \
                        -e SERVER_PORT=8081 \
                        -e JWT_SECRET=signflow-jwt-secret-key-prod-2026 \
                        -e SIGNFLOW_ENCRYPTION_KEY=signflow-encryption-key-prod-2026 \
                        ${APP_NAME}:latest

                    docker image prune -f
                """
            }
        }
    }

    post {
        success { echo 'Deploy realizado com sucesso!' }
        failure { echo 'Falha no pipeline!' }
        always { cleanWs() }
    }
}
EOF