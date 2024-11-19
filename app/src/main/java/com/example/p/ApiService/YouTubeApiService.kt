import com.example.p.Response.YouTubeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 유튜브 API에서 비디오 정보를 가져오는 요청 정의
interface YouTubeApiService {
    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String,    // 요청에 포함할 부분 (예: "snippet")
        @Query("id") videoId: String,  // 비디오 ID
        @Query("key") apiKey: String   // 유튜브 API 키
    ): Call<YouTubeResponse> // 응답을 YouTubeResponse 모델로 받음
}