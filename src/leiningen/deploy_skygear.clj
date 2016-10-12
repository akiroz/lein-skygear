(ns leiningen.deploy-skygear
  (:require [clojure.string :refer [join]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [me.raynes.fs :as fs]))


(defn- exec [print? opts & cmd]
  (let [{:keys [out err exit]} (->> (mapcat vec opts)
                                    (concat cmd)
                                    (apply sh))]
    (when print?
      (print (str out err))
      (flush))
    (when-not (= exit 0)
      (throw (Exception. (str "Command '" (join " " cmd)
                              "' exited with code: " exit))))))


(defn- setup-repo [repo source-dir static-dir]
  (when (fs/exists? repo)
    (println "Deleting existing repo...")
    (fs/delete-dir repo))
  (fs/mkdirs repo)
  (when source-dir
    (println "Copying source files...")
    (fs/copy-dir-into source-dir repo))
  (when static-dir
    (println "Copying static files...")
    (let [public_html (io/file repo "public_html")
          init-py (io/file repo "__init__.py")]
      (when-not (fs/exists? public_html)
        (fs/mkdirs public_html))
      (when-not (fs/exists? init-py)
        (fs/create init-py))
      (fs/copy-dir-into static-dir public_html))))


(defn- initialize-git [repo git-url]
  (let [git (partial exec true {:dir repo} "git")
        git-s (partial exec false {:dir repo} "git")]
    (git "init")
    (git-s "remote" "add" "skygear" git-url)
    (git-s "add" ".")
    (git "commit" "-m" "-")))


(defn- deploy-code [repo ssh-key]
  (println "Deploying to skygear cloud...")
  (let [ssh "ssh"
        env (if ssh-key
              {"GIT_SSH_COMMAND" (str ssh " -i " (fs/absolute ssh-key))}
              {"GIT_SSH_COMMAND" ssh})
        git (partial exec true {:dir repo :env env} "git")]
    (git "push" "skygear" "master" "--force")))


(defn deploy-skygear
  ([project]
   (deploy-skygear project "dev"))
  ([project profile-name]
   (let [profile (get-in project [:skygear (keyword profile-name)])
         {:keys [git-url ssh-key source-dir static-dir]} profile
         repo "target/skygear_cloud_code"]
     (when-not profile
       (throw (Exception. "Invalid profile.")))
     (when-not (and git-url (or source-dir static-dir))
       (throw (Exception. "Must provide :git-url with either/both :source-dir and :static-dir.")))
     (setup-repo repo source-dir static-dir)
     (initialize-git repo git-url)
     (deploy-code repo ssh-key)))
  ([_ _ & _]
   (throw (Exception. "Invalid number of args."))))
