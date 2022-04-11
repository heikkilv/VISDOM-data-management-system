package visdom.adapters.general.schemas


final case class FileIdSchema(
    path: String,
    project_name: String,
    host_name: String
)
