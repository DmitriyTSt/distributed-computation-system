package ru.dmitriyt.dcs.server.data.service

import kotlinx.coroutines.flow.*
import ru.dmitriyt.dcs.proto.SolverLoaderGrpcKt
import ru.dmitriyt.dcs.proto.SolverLoaderProto
import ru.dmitriyt.dcs.server.data.mapper.SolverLoaderMapper
import java.io.File

class SolverLoaderService(private val solverId: String) : SolverLoaderGrpcKt.SolverLoaderCoroutineImplBase() {

    companion object {
        private const val SOLVERS_DIR = "tasks/"
        private const val FILE_TEMPLATE = "%s.jar"
        private const val CHUNK_SIZE = 256 * 1024 // 256Кб
    }

    override fun getSolver(
        request: SolverLoaderProto.GetSolverRequest
    ): Flow<SolverLoaderProto.GetSolverResponse> {
        return File(SOLVERS_DIR + FILE_TEMPLATE.format(request.solverId))
            .readBytes()
            .toList()
            .chunked(CHUNK_SIZE)
            .asFlow()
            .map { SolverLoaderMapper.fromModelToApi(it) }
            .cancellable()
    }

    override suspend fun getCurrentSolverId(
        request: SolverLoaderProto.GetCurrentSolverIdRequest
    ): SolverLoaderProto.GetCurrentSolverIdResponse {
        return SolverLoaderProto.GetCurrentSolverIdResponse.newBuilder().setSolverId(solverId).build()
    }
}