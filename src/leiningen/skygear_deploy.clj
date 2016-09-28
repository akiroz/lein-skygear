(ns leiningen.skygear-deploy
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [me.raynes.fs :as fs]))


(defn- exec [opts & cmd]
  (->> (mapcat vec opts)
       (concat cmd)
       (apply sh)
       ((fn [{:keys [out err]}]
          (str out "\n" err)))
       println))


(defn- setup-repo [repo source-dir static-dir]
  (println "Moving files into place...")
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
  (println "Initializing git repo...")
  (let [git (partial exec {:dir repo} "git")]
    (git "init")
    (git "remote" "add" "skygear" git-url)
    (git "add" ".")
    (git "commit" "-m" "-")))


(defn- deploy-code [repo ssh-key]
  (println "Deploying to skygear cloud...")
  (let [env (if ssh-key
              {"GIT_SSH_COMMAND" (str "ssh -F /dev/null -i " ssh-key)}
              {})
        git (partial exec {:dir repo :env env} "git")]
    (git "push" "skygear" "master" "--force")))


(defn skygear-deploy
  ([project]
   (skygear-deploy project "dev"))
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
     (deploy-code repo (fs/absolute ssh-key))))
  ([_ _ & _]
   (throw (Exception. "Invalid number of args."))))
