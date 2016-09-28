# lein-skygear

A leiningen plugin that automates cloud code deployment to skygear cloud

## Install

Add the following dependency to the `:plugins` vector of your `project.clj`.

[![Clojars Project](https://img.shields.io/clojars/v/lein-skygear.svg)](https://clojars.org/lein-skygear)

Note: this plugin relies on `ssh` and `git` (>= 2.3.0) on `$PATH`.

## Usage

Add the following to `project.clj`:

```clojure

:skygear {:dev {:git-url "ssh://git@git.skygeario.com/<your-app>.git"
                ;; optional, overrides default ssh identity
                :ssh-key "path/to/private_key"
                ;; provide either or both of the following
                :source-dir "path/to/cloud_code/src"
                :static-dir "path/to/static/assets"}
          :release {...}}

```

A `__init__.py` file is expected in the `:source-dir` directory, otherwise deployment will fail.

Files inside `:static-dir` is copied to a folder named `public_html` in the cloud code repo
and accessible from the internet with `<your-app>.skygeario.com/static/`. If `:source-dir`
is not specified, an empty `__init__.py` file will be created for you.


Deploy:

```
$ lein skygear-deploy  # uses :dev by default
```
OR:
```
$ lein skygear-deploy release
```
