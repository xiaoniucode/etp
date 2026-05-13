declare namespace Api.Domain {
  interface DomainDTO {
    id: number
    domain: string
    createdAt: string
    updatedAt: string
  }

  interface DomainSearchParams {
    page: number
    size: number
  }
}