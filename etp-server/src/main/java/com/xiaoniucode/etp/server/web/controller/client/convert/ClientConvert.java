package com.xiaoniucode.etp.server.web.controller.client.convert;

import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;
import com.xiaoniucode.etp.server.web.domain.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 客户端转换器
 */
@Mapper
public interface ClientConvert {

    ClientConvert INSTANCE = Mappers.getMapper(ClientConvert.class);

    /**
     * 将Client转换为ClientDTO
     *
     * @param client 客户端实体
     * @return 客户端DTO
     */
    @Mappings({@Mapping(target = "isOnline", ignore = true)})
    ClientDTO toDTO(Client client);

    /**
     * 将Client列表转换为ClientDTO列表
     *
     * @param clients 客户端实体列表
     * @return 客户端DTO列表
     */
    List<ClientDTO> toDTOList(List<Client> clients);
}
