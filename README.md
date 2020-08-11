# A Simple Clock App

## Building

Install Node dependencies:

    npm install

### Development

    clojure -A:dev

### Release

    clojure -A:electron
    clojure -A:clock
    npm run package
