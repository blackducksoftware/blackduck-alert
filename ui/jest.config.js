module.exports = {
  testEnvironment: 'jsdom',
  moduleNameMapper: {
    '^application/(.*)$': '<rootDir>/src/main/js/application/$1',
    '^common/(.*)$': '<rootDir>/src/main/js/common/$1',
    '^page/(.*)$': '<rootDir>/src/main/js/page/$1',
    '^store/(.*)$': '<rootDir>/src/main/js/store/$1',
  },
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  collectCoverageFrom: [
    'src/**/*.{js,jsx}', // Include all JS and JSX files in the src directory
    '!src/**/*.test.{js,jsx}', // Exclude test files
    '!src/main/js/app.js', // Exclude the main app entry point
  ],
  testMatch: [
    '**/__tests__/**/*.{js,jsx}',
    '**/*.test.{js,jsx}',
  ],
};
