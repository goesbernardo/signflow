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
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t signflow-api .'
            }
        }

        stage('Deploy Render') {
            steps {
                bat """
                    curl -X POST %RENDER_DEPLOY_HOOK%
                """
            }
        }
    }
}