String hostname = "production.giraffe-cloud.biz";
SshHostAccessor prodHost = SshHostAccessor.forPassword(hostname, "admin", "l0ngN3ck");

try (HostControlSystem hcs = prodHost.open()) {
  // the path on the local machine
  Path dist = Paths.get("/opt/releases/gazelle-0.42.0.tar.gz");

  // the path on the remote machine
  Path target = hcs.getPath("/opt/gazelle/gazelle-0.42.0.tar.gz");

  MoreFiles.copyLarge(dist, target);
  Commands.execute(hcs.getCommand("tar", "xzf", target));

  MoreFiles.addPermission(target.getParent().resolve("gazelle.sh"), PosixFilePermission.OWNER_EXECUTE);
  Commands.execute(hcs.getCommand("/opt/gazelle/gazelle.sh", "start"));
}
