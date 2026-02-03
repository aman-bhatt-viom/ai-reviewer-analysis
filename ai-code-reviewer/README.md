# Getting Started with AI Code Reviewer

To integrate the AI Code Reviewer into your project, you'll need to configure the appropriate environment variables and set up build pipelines based on your Git service provider. Follow the instructions below for your specific platform.

## Note ⚠️

Assuming you have fair knowledge with your git service's environment variables setup and CI/CD pipelines integration

## Contents

## Bitbucket

### Generate read/write token

1. **Go to Access Tokens**:
   - Navigate to **`Repository settings → Access tokens`** and create a new access token.

2. **Configure the Token**:  
   - **Name it** something meaningful, like **`Eurisko AI Code Reviewer`**.  
   - **Set Permissions**: Ensure the token has **read/write** access to both the **repository** and **pull requests**.
![image](https://raw.githubusercontent.com/fouadbakkour/files/master/ai-code-review-1.png)

3. **Save the Token Securely**:  
   - 🚨 **Copy and store the token value** in a safe place. You won’t be able to view it again.
   ![image](https://raw.githubusercontent.com/fouadbakkour/files/master/ai-code-review-2.png)
   
   - 🚨 Also, **copy the full repository access URL**, formatted as:  

     ```curl
     https://x-token-auth:{TOKEN}@bitbucket.org/{WORKSPACE}/{REPO_SLUG}.git
     ```

     ![image](https://raw.githubusercontent.com/fouadbakkour/files/master/ai-code-review-3.png)
     
     **Keep this URL secure, as it grants access to your repository.**

### Enforce Branch Restrictions

To maintain data integrity and ensure the AI Code Reviewer functions properly, **avoid deleting source branches after merging**.

**Why is this important?**

If a source branch is merged into a target branch and then deleted, webhooks will fail. This failure can lead to the loss of critical data and insights, including:  
- **Incomplete or missing code reviews**
- **Disrupted coding KPI generation**  

**How to prevent source branch deletion**

To enforce this restriction, follow these steps:  

1. Navigate to **`Repository settings → Branch restrictions`**  
2. Add the necessary branch restrictions:  

   ![Branch restrictions settings](https://raw.githubusercontent.com/fouadbakkour/files/master/ai-code-review-4.png)  

3. **Disable "Allow deleting this branch"** for your protected branches:  

   ![Disable branch deletion](https://raw.githubusercontent.com/fouadbakkour/files/master/ai-code-review-5.png)

### Add Environment Variables

Navigate to `Repository settings → Repository variables` and add the following variables:

- **BITBUCKET_CODE_REVIEWER_ACCESS_TOKEN**
  - *Value:* Use the token generated from the first step.
- **CURRENT_REPO_ACCESS_GIT_URL**
  - *Value:* Use the Git URL with its token generated from the first step, formatted as: `https://x-token-auth:{READ_WRITE_TOKEN}@bitbucket.org/{WORKSPACE}/{REPO_SLUG}.git`.
- **AI_CODE_REVIEW_GIT_AUTH**
  - *Description:* Git authorization for the AI Code Reviewer engine. This will enable the AI Code Reviewer to operate within your project during reviews.
  - *Value:* <https://x-token-auth:ATCTT3xFfGN0icmlQNUuP-aE4A-k0wXbejyquA-tdGEjTX_1eNlshbj_TOdZ0QrsKPonh781AdbO4pLRDUSBYpPRVLSrRHpuMCMaUm04bU3rjZeKX6luPrLPbRwizv6ikyRs21W30G-Iyi3Y16dNdg8sii-fc7jeA3TDopm-4cmqXB3ifLRprF4=535A82E4@bitbucket.org/euriskoteam/ai-code-reviewer-engine-public.git>
- **AI_CODE_REVIEW_PROJECT**
  - *Value:* Your OpenSenthex's project ID linked to the repository.
- **SONAR_PACKAGE_FILE** (Optional)
  - If your project is not a Node-based project—such as a native Android, iOS, or Python project—or essentially any project that does not have a package.json file in its root directory:
  - On your local machine, create any .json file
  - Write inside `{"name": "your-project-name", "version": "1.0.0"}`
  - Save the file, assuming named `temp.json`
  - Open terminal window
  - `cd to/the/temp.json`
  - Cast to base64 `cat temp.json | base64`
  - Copy the output
  - Put the value under the environment variable key **SONAR_PACKAGE_FILE**

### Regular projects setup in (bitbucket-pipelines.yml)

Add the below code snippet in your `bitbucket-pipelines.yml` file

```yml
definitions:
steps:
    ... your other steps
    - step: &run_code_review
        name: Run code review for a pull request
        image: node:lts
        script:
          - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
          - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
          - cp -r ./ai-code-reviewer/run_code_review.sh "."
          - chmod +x ./run_code_review.sh
          - ./run_code_review.sh
pipelines:
    .. your other pipelines
    pull-requests:
        "**":
        - step: *run_code_review
```

### Native mobile projects setup in (bitbucket-pipelines.yml)

🚨 If your project is native Swift iOS project, please use the below script instead

```yml
definitions:
steps:
    ... your other steps
    - step: &run_code_review
        name: Run code review for a pull request
        image: norionomura/swiftlint:latest # Swift and SwiftLint precompiled image!
        script:
          - apt-get update
          - apt-get install -y curl
          - curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
          - apt-get install -y nodejs
          - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
          - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
          - cp -r ./ai-code-reviewer/run_code_review.sh "."
          - chmod +x ./run_code_review.sh
          - ./run_code_review.sh
pipelines:
    .. your other pipelines
    pull-requests:
        "**":
        - step: *run_code_review
```

🚨 If your project is native Java/Kotlin Android project, please use the below script instead

```yml
definitions:
caches:
    gradle: ~/.gradle
steps:
    ... your other steps
    - step: &run_code_review
        name: Run code review for a pull request
        image: mingc/android-build-box:1.28.0 # You might need to check further versions: https://github.com/mingchen/docker-android-build-box/tags
        size: 2x   # Allocates 8 GB of memory
        caches:
          - gradle
        script:
          - jenv global 17 # Use Java 17 or any needed java you want.
          - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
          - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
          - cp -r ./ai-code-reviewer/run_code_review.sh "."
          - chmod +x ./run_code_review.sh
          - ./run_code_review.sh
pipelines:
    .. your other pipelines
    pull-requests:
        "**":
        - step: *run_code_review
```

### Webhooks

We offer multiple webhooks to get the AI Code reviewer working with BitBucket

- **Merged Pull Requests**
  - As repository admin, visit your repo's settings and navigate to `Webhooks` section
  - Click on `Add webhook`
  - Create a title you desire
  - Put this URL in the URL box: `https://api-opensenthex.eurisko.me/webhooks/bitbucket/postMerge?apiKey={YOUR_API_KEY}`
  - Uncheck everything and Select `Merged` from the checklist ONLY!
  - Click save

## Azure

**Add Environment Variable:**

Set the following variable in your Azure DevOps environment, `[Team] → [Repo] → Pipelines → Library → Group of variables`:

- **AZURE_CODE_REVIEWER_ACCESS_TOKEN**
  - *Value:* Generate a token to authorize the AI Code Reviewer to post comments and update pull request statuses, it's basically allow you to interact with Azure REST APIs. You can create a token here: `User settings → Personal Access Tokens`. aka **PAT**
- **AI_CODE_REVIEW_GIT_AUTH**
  - *Description:* Git authorization for the AI Code Reviewer engine. This will enable the AI Code Reviewer to operate within your project during reviews.
  - *Value:* <https://x-token-auth:ATCTT3xFfGN0icmlQNUuP-aE4A-k0wXbejyquA-tdGEjTX_1eNlshbj_TOdZ0QrsKPonh781AdbO4pLRDUSBYpPRVLSrRHpuMCMaUm04bU3rjZeKX6luPrLPbRwizv6ikyRs21W30G-Iyi3Y16dNdg8sii-fc7jeA3TDopm-4cmqXB3ifLRprF4=535A82E4@bitbucket.org/euriskoteam/ai-code-reviewer-engine-public.git>
- **AI_CODE_REVIEW_PROJECT**
  - *Value:* Your OpenSenthex's project ID linked to the repository.
- **CURRENT_REPO_ACCESS_GIT_URL**
  - *Value:* The Git URL for repository access, usually including a read/write access token, formatted as: `https://${PAT}@dev.azure.com/organization/project/_git/repo`.
- **SONAR_PACKAGE_FILE** (Optional)
  - If your project is not a Node-based project—such as a native Android, iOS, or Python project—or essentially any project that does not have a package.json file in its root directory:
  - On your local machine, create any .json file
  - Write inside `{"name": "your-project-name", "version": "1.0.0"}`
  - Save the file, assuming named `temp.json`
  - Open terminal window
  - `cd to/the/temp.json`
  - Cast to base64 `cat temp.json | base64`
  - Copy the output
  - Put the value under the environment variable key **SONAR_PACKAGE_FILE**

### Regular projects setup

Add the below code snippet in your `azure-pipelines.yml` file

```yml
trigger:
- main

pr:
- main
- develop
- '*'

variables:
- group: my-variable-group

pool:
  # If you have a self-hosted agent, specify your agent pool name here
  name: 'Default' # Change 'Default' to your self-hosted agent pool name

steps:
- script: |
    git clone  $(AI_CODE_REVIEW_GIT_AUTH) ./ai-code-reviewer
    [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
    cp -r ./ai-code-reviewer/run_code_review.sh "."
    chmod +x ./run_code_review.sh
    ./run_code_review.sh
  displayName: 'Run AI Code Review Script'
  env: # Inject the environment variables to be used within the .sh script
    AI_CODE_REVIEW_GIT_AUTH: $(AI_CODE_REVIEW_GIT_AUTH)
    AI_CODE_REVIEW_PROJECT: $(AI_CODE_REVIEW_PROJECT)
    AZURE_CODE_REVIEWER_ACCESS_TOKEN: $(AZURE_CODE_REVIEWER_ACCESS_TOKEN)
    CURRENT_REPO_ACCESS_GIT_URL: $(CURRENT_REPO_ACCESS_GIT_URL)
    REPO_NAME: $(Build.Repository.Name)
    AZURE_REPOSITORY_ID: $(Build.Repository.ID)
    AZURE_PULL_REQUEST_ID: $(System.PullRequest.PullRequestId)
    AZURE_ORGANIZATION_URI: $(System.CollectionUri)
  condition: eq(variables['Build.Reason'], 'PullRequest')
```

### Native mobile projects setup

🚨 If your project is native Swift iOS project, please use the below script instead

```yml
trigger:
- main

pr:
- main
- develop
- '*'

variables:
- group: my-variable-group

pool:
  name: 'Default' # Change 'Default' to your self-hosted agent pool name

container:
  image: norionomura/swiftlint:latest

steps:
- script: |
    apt-get update
    apt-get install -y curl
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
  displayName: 'Install Node.js'

- script: |
    [ -n $(SONAR_PACKAGE_FILE) ] && echo $(SONAR_PACKAGE_FILE) | base64 --decode > package.json || true
    git clone  $(AI_CODE_REVIEW_GIT_AUTH) ./ai-code-reviewer
    cp -r ./ai-code-reviewer/run_code_review.sh "."
    chmod +x ./run_code_review.sh
    ./run_code_review.sh
  displayName: 'Run AI Code Review Script'
  env: # Inject the environment variables to be used within the .sh script
    AI_CODE_REVIEW_GIT_AUTH: $(AI_CODE_REVIEW_GIT_AUTH)
    AI_CODE_REVIEW_PROJECT: $(AI_CODE_REVIEW_PROJECT)
    AZURE_CODE_REVIEWER_ACCESS_TOKEN: $(AZURE_CODE_REVIEWER_ACCESS_TOKEN)
    CURRENT_REPO_ACCESS_GIT_URL: $(CURRENT_REPO_ACCESS_GIT_URL)
    REPO_NAME: $(Build.Repository.Name)
    AZURE_REPOSITORY_ID: $(Build.Repository.ID)
    AZURE_PULL_REQUEST_ID: $(System.PullRequest.PullRequestId)
    AZURE_ORGANIZATION_URI: $(System.CollectionUri)
  condition: eq(variables['Build.Reason'], 'PullRequest')
```

🚨 If your project is native Java/Kotlin Android project, please use the below script instead

```yml
trigger:
- main

pr:
- main
- develop
- '*'

variables:
- group: my-variable-group

pool:
  name: 'Default' # Change 'Default' to your self-hosted agent pool name

container:
  image: mingc/android-build-box:1.28.0 # You might need to check further versions: https://github.com/mingchen/docker-android-build-box/tags

steps:
- script: |
    apt-get update
    apt-get install -y curl
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
  displayName: 'Install Node.js'

- script: |
    [ -n $(SONAR_PACKAGE_FILE) ] && echo $(SONAR_PACKAGE_FILE) | base64 --decode > package.json || true
    jenv global 17
    git clone  $(AI_CODE_REVIEW_GIT_AUTH) ./ai-code-reviewer
    cp -r ./ai-code-reviewer/run_code_review.sh "."
    chmod +x ./run_code_review.sh
    ./run_code_review.sh
  displayName: 'Run AI Code Review Script'
  env: # Inject the environment variables to be used within the .sh script
    AI_CODE_REVIEW_GIT_AUTH: $(AI_CODE_REVIEW_GIT_AUTH)
    AI_CODE_REVIEW_PROJECT: $(AI_CODE_REVIEW_PROJECT)
    AZURE_CODE_REVIEWER_ACCESS_TOKEN: $(AZURE_CODE_REVIEWER_ACCESS_TOKEN)
    CURRENT_REPO_ACCESS_GIT_URL: $(CURRENT_REPO_ACCESS_GIT_URL)
    REPO_NAME: $(Build.Repository.Name)
    AZURE_REPOSITORY_ID: $(Build.Repository.ID)
    AZURE_PULL_REQUEST_ID: $(System.PullRequest.PullRequestId)
    AZURE_ORGANIZATION_URI: $(System.CollectionUri)
  condition: eq(variables['Build.Reason'], 'PullRequest')
```

## Gitlab

**Add Environment Variable:**

Set the following variable in your Gitlab `[Group] → [Repository] → CI/CD Settings`:

- **GITLAB_CODE_REVIEWER_ACCESS_TOKEN**
  - *Value:* Generate a token to authorize the AI Code Reviewer to post comments and update pull request statuses, it's basically allow you to interact with Gitlab REST APIs. You can create a token here: `[Group] [Repository] → Access tokens`. aka **PAT**
- **AI_CODE_REVIEW_GIT_AUTH**
  - *Description:* Git authorization for the AI Code Reviewer engine. This will enable the AI Code Reviewer to operate within your project during reviews.
  - *Value:* <https://x-token-auth:ATCTT3xFfGN0icmlQNUuP-aE4A-k0wXbejyquA-tdGEjTX_1eNlshbj_TOdZ0QrsKPonh781AdbO4pLRDUSBYpPRVLSrRHpuMCMaUm04bU3rjZeKX6luPrLPbRwizv6ikyRs21W30G-Iyi3Y16dNdg8sii-fc7jeA3TDopm-4cmqXB3ifLRprF4=535A82E4@bitbucket.org/euriskoteam/ai-code-reviewer-engine-public.git>
- **AI_CODE_REVIEW_PROJECT**
  - *Value:* Your OpenSenthex's project ID linked to the repository.
- **CURRENT_REPO_ACCESS_GIT_URL**
  - *Value:* The Git URL for repository access, usually including a read/write access token, formatted as: `https://oauth2:{PAT}@gitlab.com/{GROUP}/{REPO}.git`.
- **SONAR_PACKAGE_FILE** (Optional)
  - If your project is not a Node-based project—such as a native Android, iOS, or Python project—or essentially any project that does not have a package.json file in its root directory:
  - On your local machine, create any .json file
  - Write inside `{"name": "your-project-name", "version": "1.0.0"}`
  - Save the file, assuming named `temp.json`
  - Open terminal window
  - `cd to/the/temp.json`
  - Cast to base64 `cat temp.json | base64`
  - Copy the output
  - Put the value under the environment variable key **SONAR_PACKAGE_FILE**

### Regular projects setup

Add the below code snippet in your `.gitlab-ci.yml` file

```yml
image: node:18  # Specify the Docker image you want to use
stages:
- build
- test
- deploy

run_code_review:
stage: build
script:
  - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
  - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
  - cp -r ./ai-code-reviewer/run_code_review.sh "."
  - chmod +x ./run_code_review.sh
  - ./run_code_review.sh
only:
  - merge_requests
```

### Native mobile projects setup

🚨 If your project is native Swift iOS project, please use the below script instead

```yml
image: norionomura/swiftlint:latest  # Specify the Docker image you want to use

stages:
- build
- test
- deploy

run_code_review:
  stage: build
  script:
    - apt-get update
    - apt-get install -y curl
    - curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    - apt-get install -y nodejs
    - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
    - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
    - cp -r ./ai-code-reviewer/run_code_review.sh "."
    - chmod +x ./run_code_review.sh
    - ./run_code_review.sh
  only:
    - merge_requests
```

🚨 If your project is native Java/Kotlin Android project, please use the below script instead

```yml
image: mingc/android-build-box:1.28.0 # You might need to check further versions: https://github.com/mingchen/docker-android-build-box/tags

stages:
- build
- test
- deploy

run_code_review:
  stage: build
  script:
    - apt-get update
    - apt-get install -y curl
    - curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    - apt-get install -y nodejs
    - [ -n "$SONAR_PACKAGE_FILE" ] && echo "$SONAR_PACKAGE_FILE" | base64 --decode > package.json || true
    - jenv global 17 # Use Java 17 or any needed java you want.
    - git clone $AI_CODE_REVIEW_GIT_AUTH ./ai-code-reviewer
    - cp -r ./ai-code-reviewer/run_code_review.sh "."
    - chmod +x ./run_code_review.sh
    - ./run_code_review.sh
  only:
    - merge_requests
```

## Github

**Add Environment Variable:**

Set the following variable in your Github `Repository → Settings → Secrets and variables → Actions → Secrets`:

- **CODE_REVIEWER_GITHUB_ACCESS_TOKEN**
  - *Value:* Generate a token to authorize the AI Code Reviewer to post comments and update pull request statuses, it's basically allow you to interact with Github REST APIs. You can create a token here: <https://github.com/settings/tokens>.
- **AI_CODE_REVIEW_GIT_AUTH**
  - *Description:* Git authorization for the AI Code Reviewer engine. This will enable the AI Code Reviewer to operate within your project during reviews.
  - *Value:* <https://x-token-auth:ATCTT3xFfGN0icmlQNUuP-aE4A-k0wXbejyquA-tdGEjTX_1eNlshbj_TOdZ0QrsKPonh781AdbO4pLRDUSBYpPRVLSrRHpuMCMaUm04bU3rjZeKX6luPrLPbRwizv6ikyRs21W30G-Iyi3Y16dNdg8sii-fc7jeA3TDopm-4cmqXB3ifLRprF4=535A82E4@bitbucket.org/euriskoteam/ai-code-reviewer-engine-public.git>
- **AI_CODE_REVIEW_PROJECT**
  - *Value:* Your OpenSenthex's project ID linked to the repository.
- **SONAR_PACKAGE_FILE** (Optional)
  - If your project is not a Node-based project—such as a native Android, iOS, or Python project—or essentially any project that does not have a package.json file in its root directory:
  - On your local machine, create any .json file
  - Write inside `{"name": "your-project-name", "version": "1.0.0"}`
  - Save the file, assuming named `temp.json`
  - Open terminal window
  - `cd to/the/temp.json`
  - Cast to base64 `cat temp.json | base64`
  - Copy the output
  - Put the value under the environment variable key **SONAR_PACKAGE_FILE**

### Regular projects setup

Add the below code snippet in your `.github/workflows/code_review.yml` file

```yml
name: Run Code Review for Pull Requests

on:
  pull_request:
    branches:
      - '**'
permissions: write-all
jobs:
  code_review:
    name: Run code review for a pull request
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 'lts/*'
      
      - name: Clone AI code review repository
        run: git clone ${{ secrets.AI_CODE_REVIEW_GIT_AUTH }} ./ai-code-reviewer
      
      - name: Copy and run the code review script
        env:
          CODE_REVIEWER_GITHUB_ACCESS_TOKEN: ${{ secrets.CODE_REVIEWER_GITHUB_ACCESS_TOKEN }}
          AI_CODE_REVIEW_PROJECT: ${{ secrets.AI_CODE_REVIEW_PROJECT }}
        run: |
          [ -n ${{ secrets.SONAR_PACKAGE_FILE }} ] && echo ${{ secrets.SONAR_PACKAGE_FILE }} | base64 --decode > package.json || true
          cp -r ./ai-code-reviewer/run_code_review.sh .
          chmod +x ./run_code_review.sh
          ./run_code_review.sh
```

### Native mobile projects setup

🚨 If your project is native Swift iOS project, please use the below script instead

```yml
name: Run Code Review for Pull Requests

on:
  pull_request:
    branches:
      - '**'
permissions: write-all
jobs:
  code_review:
    name: Run code review for a pull request
    runs-on: ubuntu-latest
    container:
      image: norionomura/swiftlint:latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Install Node.js
        run: |
          apt-get update
          apt-get install -y curl
          curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
          apt-get install -y nodejs
      
      - name: Clone AI code review repository
        run: git clone ${{ secrets.AI_CODE_REVIEW_GIT_AUTH }} ./ai-code-reviewer
      
      - name: Copy and run the code review script
        env:
          CODE_REVIEWER_GITHUB_ACCESS_TOKEN: ${{ secrets.CODE_REVIEWER_GITHUB_ACCESS_TOKEN }}
          AI_CODE_REVIEW_PROJECT: ${{ secrets.AI_CODE_REVIEW_PROJECT }}
        run: |
          [ -n ${{ secrets.SONAR_PACKAGE_FILE }} ] && echo ${{ secrets.SONAR_PACKAGE_FILE }} | base64 --decode > package.json || true
          cp -r ./ai-code-reviewer/run_code_review.sh .
          chmod +x ./run_code_review.sh
          ./run_code_review.sh
```

🚨 If your project is native Java/Kotlin Android project, please use the below script instead

```yml
name: Run Code Review for Pull Requests

on:
  pull_request:
    branches:
      - '**'
permissions: write-all
jobs:
  code_review:
    name: Run code review for a pull request
    runs-on: ubuntu-latest
    container:
      image: mingc/android-build-box:1.28.0 # You might need to check further versions: https://github.com/mingchen/docker-android-build-box/tags
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Install Node.js
        run: |
          apt-get update
          apt-get install -y curl
          curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
          apt-get install -y nodejs
      
      - name: Clone AI code review repository
        run: git clone ${{ secrets.AI_CODE_REVIEW_GIT_AUTH }} ./ai-code-reviewer
      
      - name: Copy and run the code review script
        env:
          CODE_REVIEWER_GITHUB_ACCESS_TOKEN: ${{ secrets.CODE_REVIEWER_GITHUB_ACCESS_TOKEN }}
          AI_CODE_REVIEW_PROJECT: ${{ secrets.AI_CODE_REVIEW_PROJECT }}
        run: |
          [ -n ${{ secrets.SONAR_PACKAGE_FILE }} ] && echo ${{ secrets.SONAR_PACKAGE_FILE }} | base64 --decode > package.json || true
          jenv global 17
          cp -r ./ai-code-reviewer/run_code_review.sh .
          chmod +x ./run_code_review.sh
          ./run_code_review.sh
```

## JIRA

On OpenSenthex, make sure to provided the needed details to connect your project to Jira;
Mainly they are:

- Jira username (Email)
- Jira token (Can be generated on your account from here: <https://id.atlassian.com/manage-profile/security/api-tokens>)
- Jira base URL (e.g: <https://something.atlassian.net>)
- Jira bug tickets rules
- Jira user story rules
- Jira project key (e.g: OP)

### Comments on tickets

OpenSenthex's AI Code Reviewer will validate the pull requests against the linked  Jira tickets when it finds violations or none-violations.

It will also check Jira tickets if they meets the rules you put on OpenSenthex and post comments on them automatically.

### JIRA Sprint validation

OpenSenthex offers a webhook to validate Jira sprints when they starts.

The current webhook URL is:
<https://api-opensenthex.eurisko.me/webhooks/jira/onSprintStart?apiKey=KEY>

As for the key, the details should be found on your OpenSenthex's account.

Note: Only JIRA's administrators can configure webhooks

### Contribution

In order to debug the engine:

- Clone the engine's private code
- run `npm install`
- Set `DEBUG` to true in `src/constants.ts`
- Go to `run_code_review.sh` and change the `TARGET_DIR` to your target local project
- run `./run_code_review.sh`
