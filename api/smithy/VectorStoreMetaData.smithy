namespace io.github.quafadas.dairect

map VectorStoreMetaData {
  key: String
  value: String
}

map AssistantMetaData {
  key: String
  value: String
}

map ThreadMetaData {
  key: String
  value: String
}

list CodeInterpreterFileIds {
    member: String
}

list VectorStoreFileIds {
    member: String
}

list VectorStoreIds {
    member: String
}

list FileIds {
    member: String
}