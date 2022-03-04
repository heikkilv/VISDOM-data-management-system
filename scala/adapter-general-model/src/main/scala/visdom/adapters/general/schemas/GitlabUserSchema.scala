package visdom.adapters.general.schemas


final case class GitlabUserEventSchema(
    hostName: String,
    eventId: String,
    eventType: String,
    userSchema: PipelineUserSchema
)
