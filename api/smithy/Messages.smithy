$version: "2.0"

namespace io.github.quafadas.dairect

use alloy#discriminated
use alloy#untagged

@untagged
union MessageOnThread {
  str: String
  array: MessageToSendList
}

list MessageContentList {
  member: MessageContent
}

list MessageToSendList {
  member: MessageToSend
}

@discriminated("type")
union MessageContent {
  text: Text,
  image_file: ImageFile,
  image_url: ImageUrl
}

structure Text {
  @required
  text: TextValue
}

@discriminated("type")
union Annotation {
  text: Text,
  image_file: ImageFile,
  image_url: ImageUrl
}

structure TextValue {
  value: String,

}

@discriminated("type")
union MessageToSend {
  text: TextToSend
  image_file: ImageFile
  image_url: ImageUrl
}

structure TextToSend {
    @required
    text: String
}

structure ImageFile {
  @required image_file: ImageDetails
}

structure ImageDetails {
  @required file_id: String,
  detail: String
}


structure ImageUrl {
  @required image_url: ImageUrlDetails
}

structure ImageUrlDetails {
  @required url: String,
  detail: String
}


