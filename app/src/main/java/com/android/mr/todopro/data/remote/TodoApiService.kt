package com.android.mr.todopro.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoApiService {

    // JSONPlaceholder supports filtering by query parameter out of the box:
    // GET https://jsonplaceholder.typicode.com/todos?userId=1

    // Add userId to:
    //  - DTO,
    //  - Entity,
    //  - domain model
    // and filter at the API layer — not in the UI.

    @GET("todos")
    suspend fun getTodos(@Query("userId") userId: Int? = null): List<TodoDto>

    @GET("todos/{id}")
    suspend fun getTodo(@Path("id") id: Int): TodoDto

    // JSONPlaceholder supports these but doesn't actually persist - good enough for learning
    @POST("todos")
    suspend fun createTodo(@Body todo: TodoDto): TodoDto

    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id : Int, @Body todo: TodoDto): TodoDto

    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id : Int) : Response<Unit>

}