package global.msnthrp.mokshan.usecase

import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val useCaseModule = module {
    singleOf(::PhrasebookUseCase)
}