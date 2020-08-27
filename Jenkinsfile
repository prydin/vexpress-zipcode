pipeline {
  agent {
    docker {
      image 'prydin/ci-build-jdk11'
    }

  }
  stages {
    stage('Build') {
      steps {
        sh './gradlew build'
      }
    }

    stage('Test') {
      steps {
        sh './gradlew test'
      }
    }

  }
}