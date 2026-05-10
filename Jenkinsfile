pipeline {
    agent any

    tools {
        dockerTool 'docker'
    }

    environment {
        DOCKER_IMAGE       = 'goesbernardo/signflow'
        DOCKER_TAG         = "${BUILD_NUMBER}"
        PATH               = "/usr/local/bin:/usr/bin:/bin:${env.PATH}"
        DOCKER_API_VERSION = '1.40'
    }

    stages {

        // ── Checkout ──────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Build: #${BUILD_NUMBER}"
            }
        }

        // ── Build ─────────────────────────────────────────────────
        stage('Build') {
            steps {
                sh '''
                    chmod +x ./mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        // ── Testes ────────────────────────────────────────────────
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── Docker Build ──────────────────────────────────────────
        stage('Docker Build') {
            steps {
                sh '''
                    echo "Construindo imagem Docker..."
                    docker build \
                        -t $DOCKER_IMAGE:$DOCKER_TAG \
                        -t $DOCKER_IMAGE:latest \
                        .
                    echo "Imagem construida: $DOCKER_IMAGE:$DOCKER_TAG"
                '''
            }
        }

        // ── Docker Push ───────────────────────────────────────────
        // --password-stdin nao suportado no Docker 17.05
        // Usando docker login com -p diretamente
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "Autenticando no Docker Hub..."
                        docker login -u "$DOCKER_USER" -p "$DOCKER_PASS"

                        echo "Enviando imagens..."
                        docker push $DOCKER_IMAGE:$DOCKER_TAG
                        docker push $DOCKER_IMAGE:latest

                        docker logout
                        echo "Push concluido: $DOCKER_IMAGE:$DOCKER_TAG"
                    '''
                }
            }
        }

        // ── Deploy Render (somente branch main) ───────────────────
        stage('Deploy Render') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([string(
                    credentialsId: 'render-deploy-hook-url',
                    variable: 'DEPLOY_HOOK'
                )]) {
                    sh '''
                        echo "Acionando deploy no Render..."
                        curl -X POST "$DEPLOY_HOOK" \
                             -H "Content-Type: application/json" \
                             --fail \
                             --silent \
                             --show-error
                        echo "Deploy acionado com sucesso."
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline concluído — imagem: $DOCKER_IMAGE:$DOCKER_TAG"
        }
        failure {
            echo "❌ Pipeline falhou no build #${BUILD_NUMBER}"
        }
        always {
            node('') {
                cleanWs()
            }
        }
    }
}