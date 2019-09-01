import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.AIListener
import ai.api.AIServiceException
import ai.api.android.AIService
import ai.api.model.AIError
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask

/*
* Implemented By Issa Loubani 1/9/2019
*   Notes :
*   Be sure before you use this API TO :
*  1- Add Internet Permission To Your Manifest  <uses-permission android:name="android.permission.INTERNET" />
*
*  2- Add the following library in your gradle build :
*
    // some another dependencies...
    implementation 'ai.api:libai:1.6.11'
    implementation 'ai.api:sdk:2.0.5@aar'
    // api.ai SDK dependencies
    implementation 'com.google.code.gson:gson:2.8.1'
    implementation 'commons-io:commons-io:2.4'
    implementation 'androidx.recyclerview:recyclerview:1.0.0'
 * More on my Github : issaLoubani99
 */

class DialogFlowAPI : AIListener {

    // vars

    private var config: ai.api.android.AIConfiguration? = null // handle Ai config

    @SuppressLint("StaticFieldLeak")
    private var aiService: AIService? = null

    private var aiDataService: AIDataService? = null

    private var aiRequest: AIRequest? = null

    // implemented methods
    override fun onResult(result: AIResponse?) {}

    override fun onListeningStarted() {}

    override fun onAudioLevel(level: Float) {}

    override fun onError(error: AIError?) {}

    override fun onListeningCanceled() {}

    override fun onListeningFinished() {}

    constructor(context: Context, clientAccessToken: String) {

        // init AI Config
        this.config = ai.api.android.AIConfiguration(
            clientAccessToken,
            AIConfiguration.SupportedLanguages.English,
            ai.api.android.AIConfiguration.RecognitionEngine.System
        )

        // init ai service to use network
        aiService = AIService.getService(context, config)
        aiService!!.setListener(this)

        // handle ai data
        this.aiDataService = AIDataService(config)

        // handle ai request
        this.aiRequest = AIRequest()

    }

    /*
    * Send Message
     */
    fun sendMessage(msg: String, onSuccess: (String) -> Unit) {
        aiService!!.startListening()
        aiRequest!!.setQuery(msg)

        // create request task class
        object : AsyncTask<AIRequest, Void, AIResponse>() {

            override fun doInBackground(vararg aiRequests: AIRequest): AIResponse? {
                val request = aiRequests[0]
                try {

                    return aiDataService!!.request(aiRequest)
                } catch (e: AIServiceException) {
                }

                return null
            }

            override fun onPostExecute(response: AIResponse?) {
                if (response != null) {

                    val result = response.result

                    // value in string
                    val reply = result.fulfillment.speech

                    // on success
                    onSuccess(reply)

                    // on finish
                    aiService!!.stopListening()
                }
            }
        }.execute(aiRequest)
    }
}