package com.dicoding.academies.data

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dicoding.academies.data.source.local.LocalDataSource
import com.dicoding.academies.data.source.local.entity.CourseEntity
import com.dicoding.academies.data.source.local.entity.CourseWithModule
import com.dicoding.academies.data.source.local.entity.ModuleEntity
import com.dicoding.academies.data.source.remote.ApiResponse
import com.dicoding.academies.data.source.remote.RemoteDataSource
import com.dicoding.academies.data.source.remote.response.ContentResponse
import com.dicoding.academies.data.source.remote.response.CourseResponse
import com.dicoding.academies.data.source.remote.response.ModuleResponse
import com.dicoding.academies.utils.AppExecutors
import com.dicoding.academies.vo.Resource


class AcademyRepository private constructor(private val remoteDataSource: RemoteDataSource, private val localData: LocalDataSource, private val appExecutors: AppExecutors) : AcademyDataSource {

    companion object {
        @Volatile
        private var instance: AcademyRepository? = null

        fun getInstance(remoteData: RemoteDataSource, localData: LocalDataSource, appExecutors: AppExecutors): AcademyRepository =
                instance ?: synchronized(this) {
                    instance ?: AcademyRepository(remoteData, localData, appExecutors).apply {
                        instance = this
                    }
                }
    }

    override fun getAllCourses(): LiveData<Resource<PagedList<CourseEntity>>> {
        return object : NetworkBoundResource<PagedList<CourseEntity>, List<CourseResponse>>(appExecutors) {
            override fun loadFromDB(): LiveData<PagedList<CourseEntity>> {
                val config = PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(4)
                        .setPageSize(4)
                        .build()
                return LivePagedListBuilder(localData.getAllCourses(), config).build()
            }

            override fun shouldFetch(data: PagedList<CourseEntity>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun createCall(): LiveData<ApiResponse<List<CourseResponse>>> {
                return remoteDataSource.getAllCourses()
            }

            override fun saveCallResult(data: List<CourseResponse>) {
                val courseList = ArrayList<CourseEntity>()
                for (response in data) {
                    val course = CourseEntity(
                            response.id,
                            response.title,
                            response.description,
                            response.date,
                            false,
                            response.imagePath
                    )
                    courseList.add(course)
                }
                localData.insertCourses(courseList)
            }
        }.asLiveData()
    }

    override fun getBookmarkedCourses(): LiveData<PagedList<CourseEntity>> {
        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(4)
                .setPageSize(4)
                .build()
        return LivePagedListBuilder(localData.getBookmarkedCourses(), config).build()
    }

    override fun setCourseBookmark(course: CourseEntity, state: Boolean) {
        appExecutors.diskIO().execute {
            localData.setCourseBookmark(course, state)
        }
    }

    override fun setReadModule(module: ModuleEntity) {
        appExecutors.diskIO().execute {
            localData.setReadModule(module)
        }
    }

    override fun getCourseWithModules(courseId: String): LiveData<Resource<CourseWithModule>> {
        return object : NetworkBoundResource<CourseWithModule, List<ModuleResponse>>(appExecutors) {
            override fun loadFromDB(): LiveData<CourseWithModule> {
                return localData.getCourseWithModules(courseId)
            }

            override fun shouldFetch(data: CourseWithModule?): Boolean {
                return data?.mModules == null || data.mModules.isEmpty()
            }

            override fun createCall(): LiveData<ApiResponse<List<ModuleResponse>>> {
                return remoteDataSource.getModules(courseId)
            }

            override fun saveCallResult(data: List<ModuleResponse>) {
                val moduleList = ArrayList<ModuleEntity>()
                for (response in data) {
                    val course = ModuleEntity(
                            response.moduleId,
                            response.courseId,
                            response.title,
                            response.position,
                            false
                    )
                    moduleList.add(course)
                }
                localData.insertModules(moduleList)
            }

        }.asLiveData()
    }

    override fun getAllModulesByCourse(courseId: String): LiveData<Resource<List<ModuleEntity>>> {
        return object : NetworkBoundResource<List<ModuleEntity>, List<ModuleResponse>>(appExecutors) {
            override fun loadFromDB(): LiveData<List<ModuleEntity>> {
                return localData.getAllModulesByCourse(courseId)
            }

            override fun shouldFetch(data: List<ModuleEntity>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun createCall(): LiveData<ApiResponse<List<ModuleResponse>>> {
                return remoteDataSource.getModules(courseId)
            }

            override fun saveCallResult(data: List<ModuleResponse>) {
                val moduleList = ArrayList<ModuleEntity>()
                for (response in data) {
                    val course = ModuleEntity(
                            response.moduleId,
                            response.courseId,
                            response.title,
                            response.position,
                            false
                    )
                    moduleList.add(course)
                }
                localData.insertModules(moduleList)
            }

        }.asLiveData()
    }

    override fun getContent(moduleId: String): LiveData<Resource<ModuleEntity>> {
        return object : NetworkBoundResource<ModuleEntity, ContentResponse>(appExecutors) {
            override fun loadFromDB(): LiveData<ModuleEntity> {
                return localData.getModuleWithContent(moduleId)
            }

            override fun shouldFetch(data: ModuleEntity?): Boolean {
                return data?.contentEntity == null
            }

            override fun createCall(): LiveData<ApiResponse<ContentResponse>> {
                return remoteDataSource.getContent(moduleId)
            }

            override fun saveCallResult(data: ContentResponse) {
                localData.updateContent(data.content, moduleId)
            }

        }.asLiveData()
    }
}

