module.exports = {
  testEnvironment: 'jsdom',
  moduleNameMapper: {
    '^application/(.*)$': '<rootDir>/src/main/js/application/$1',
    '^common/(.*)$': '<rootDir>/src/main/js/common/$1',
    '^page/(.*)$': '<rootDir>/src/main/js/page/$1',
    '^store/(.*)$': '<rootDir>/src/main/js/store/$1',
    '^test/(.*)$': '<rootDir>/test/$1',
    '^_theme$': '<rootDir>/src/main/js/_theme',
  },
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  collectCoverageFrom: [
    'src/**/*.{js,jsx}',
    '!src/**/*.test.{js,jsx}',
    '!src/main/js/Index.js',
  ],
  testMatch: [
    '**/__tests__/**/*.{js,jsx}',
    '**/*.test.{js,jsx}',
  ],
};
