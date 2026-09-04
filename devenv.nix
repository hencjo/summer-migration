{ pkgs, lib, config, inputs, ... }:

{
  packages = with pkgs;[
    git
    gh
    gnupg
  ];

  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk8;
    maven.enable = true;
  };

  scripts.build.exec = ''
    mvn package
  '';

  scripts.release.exec = ''
    ./scripts/release.sh "$@"
  '';

  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  # Removes trailing whitespace without reformatting indentation.
  git-hooks.hooks.trim-trailing-whitespace.enable = true;
}
