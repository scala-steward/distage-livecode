package livecode

import distage.ModuleDef
import izumi.distage.docker.Docker
import izumi.distage.docker.Docker.DockerPort
import izumi.distage.docker.examples.PostgresDocker
import izumi.distage.docker.modules.DockerContainerModule
import livecode.code.Postgres.PostgresPortCfg
import zio.Task

object PostgresDockerModule extends ModuleDef {
  // launch postgres docker for tests
  make[PostgresDocker.Container]
    .fromResource(PostgresDocker.make[Task])

  // spawned docker container port is dynamic
  // to prevent conflicts. Make PostgresPortCfg
  // point to the new port. This will also
  // cause the container to start before
  // integration check is performed
  make[PostgresPortCfg].from {
    docker: PostgresDocker.Container =>
      val knownAddress = docker.availablePorts.availablePorts(DockerPort.TCP(5432)).head
      PostgresPortCfg(knownAddress.hostV4, knownAddress.port)
  }

  // add docker dependencies and override default configuration
  include(new DockerContainerModule[Task] overridenBy new ModuleDef {
    make[Docker.ClientConfig].from {
      Docker.ClientConfig(
        readTimeoutMs    = 500,
        connectTimeoutMs = 500,
        allowReuse       = true,
        useRemote        = false,
        useRegistry      = false,
        remote           = None,
        registry         = None,
      )
    }
  })
}
