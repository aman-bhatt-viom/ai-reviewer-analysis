
if [ -n "$CI" ]; then
    echo "🚗 Running on CI"
    # Commands to run ONLY on CI
    echo "🚀 Installing sonarqube-scanner globally"
    npm install sonarqube-scanner@4.2.3 -g
    echo "🚀 Changing directory to ai-code-reviewer folder"
    cd ai-code-reviewer
    echo "🚀 Installing ai-code-reviewer node dependencies"
    npm install
    echo "🚀 Setting FIRST_RUN to true!"
    export FIRST_RUN=true
    echo "🚀 Running ai-code-reviewer main function"
    node engine_library/main.js
else
    # The below project is linked to: https://opensenthex.eurisko.me/projects/66e2b36b4688a42ff1b35334
    echo "🚗 Not running on CI"
    TARGET_DIR=/Users/fouadbakour/Desktop/react-native-with-fastlane
    # TARGET_DIR=/Users/fouadbakour/Desktop/temp/eurisko-platform-uicomponents-react-native
    rm -rf $TARGET_DIR/ai-code-reviewer
    mkdir -p $TARGET_DIR/ai-code-reviewer && cp -R . $TARGET_DIR/ai-code-reviewer
    cd $TARGET_DIR/ai-code-reviewer
    export FIRST_RUN=true
    npm run start || rm -rf $TARGET_DIR/ai-code-reviewer
    rm -rf $TARGET_DIR/ai-code-reviewer
fi