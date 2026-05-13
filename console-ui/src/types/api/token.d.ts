declare namespace Api.AccessToken {
  interface AccessTokenDTO {
    id: number
    name: string
    token: string
    createdAt: string
    updatedAt: string
  }

  interface AccessTokenCreateParam {
    name: string
  }

  interface AccessTokenUpdateParam {
    id: number
    name: string
  }
}
