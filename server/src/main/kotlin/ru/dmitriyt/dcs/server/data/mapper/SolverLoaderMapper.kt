package ru.dmitriyt.dcs.server.data.mapper

import com.google.protobuf.ByteString
import ru.dmitriyt.dcs.proto.SolverLoaderProto

object SolverLoaderMapper {

    fun fromModelToApi(data: List<Byte>): SolverLoaderProto.GetSolverResponse {
        return SolverLoaderProto.GetSolverResponse
            .newBuilder()
            .setData(ByteString.copyFrom(data.toByteArray()))
            .build()
    }
}