pipeline {

    agent any

    tools {
        maven 'maven3'
    }

    environment {
        RENDER_DEPLOY_HOOK = credentials('render-deploy-hook')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Tests') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t signflow-api .'
            }
        }

        stage('Deploy Render') {
            steps {
                sh """
                    curl -X POST $RENDER_DEPLOY_HOOK
                """
            }
        }
    }
}