# A Simple Clock App

Simple dual window clock app with countdown timer and selectable alarm sound. One window is for displaying the clock, the other window is for controlling how the clock behaves.

## Building

Install Node dependencies:
```
npm install
```

### Developmen

```
clojure -A:dev
```
### Release

```
clojure -A:electron
clojure -A:clock
clojure -A:control
npm run package
```
or just
```
clean.sh
build.sh
```