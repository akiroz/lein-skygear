(defproject lein-skygear "0.1.5"
  :description "A leiningen plugin that automates cloud code deployment to skygear cloud"
  :url "https://github.com/akiroz/lein-skygear"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :eval-in-leiningen true
  :dependencies [[me.raynes/fs "1.4.6"]]

  :skygear {:test {:git-url "ssh://git@git.skygeario.com/leinskygeartest.git"
                   :source-dir "test/code"
                   :static-dir "test/static"}}

  )
