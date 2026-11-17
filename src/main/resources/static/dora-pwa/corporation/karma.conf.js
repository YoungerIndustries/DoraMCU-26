module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {
      },
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/corporation'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' }
      ]
    },
    reporters: ['progress'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    customLaunchers: {
      ChromeHeadless: {
        base: 'ChromeHeadless',
        flags: [
          '--single-process',
          '--no-sandbox',
          '--disable-gpu',
          '--disable-software-rasterizer',
          '--disable-dev-shm-usage',
          '--no-proxy-server'
        ]
      }
    },
    singleRun: false,
    restartOnFileChange: true,
    browserDisconnectTimeout: 30000,
    browserNoActivityTimeout: 120000,
    captureTimeout: 120000
  });
};
