# A Simple Clock App


## Running it

```shell
npm install --save-dev electron # install electron binaries
```

### Emacs REPL
```shell
lein cljsbuild once #Compile cljs e.g. main.js
```

<kbd>M-x cider-jack-in-cljs</kbd>
<kbd>figwheel</kbd>

```shell
npm start
```

## Releasing

```shell
lein do clean, cljsbuild once frontend-release, cljsbuild once electron-release
npm start # start electron to test that everything works
```

After that follow the [distribution guide for the electron.](https://github.com/atom/electron/blob/master/docs/tutorial/application-distribution.md)

The easiest way to package an electron app is by using [electron-packager](https://github.com/maxogden/electron-packager):

```shell
npm install electron-packager -g                                            # install electron packager
electron-packager . HelloWorld --platform=darwin --arch=x64 --electron-version=1.4.8 # package it!
```
