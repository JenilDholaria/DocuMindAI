
package com.documind.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<SavedChatSource> = emptyList()
)

data class SavedChatSource(
    val page: Int,
    val excerpt: String
)

data class SavedChat(
    val id: String,
    val title: String,
    val documentId: String,
    val documentName: String,
    val messages: List<SavedChatMessage>,
    val updatedAt: Long
)

object ChatHistoryStore {

    private const val PREFS = "documind_chat_history"
    private const val KEY = "chats"

    fun getAll(context: Context): List<SavedChat> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<SavedChat>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val messageArray = item.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<SavedChatMessage>()

                for (j in 0 until messageArray.length()) {
                    val message = messageArray.getJSONObject(j)
                    val sourceArray = message.optJSONArray("sources") ?: JSONArray()
                    val sources = mutableListOf<SavedChatSource>()

                    for (k in 0 until sourceArray.length()) {
                        val source = sourceArray.getJSONObject(k)
                        sources.add(
                            SavedChatSource(
                                page = source.optInt("page"),
                                excerpt = source.optString("excerpt")
                            )
                        )
                    }

                    messages.add(
                        SavedChatMessage(
                            text = message.optString("text"),
                            isUser = message.optBoolean("isUser"),
                            sources = sources
                        )
                    )
                }

                result.add(
                    SavedChat(
                        id = item.optString("id"),
                        title = item.optString("title", "New chat"),
                        documentId = item.optString("documentId"),
                        documentName = item.optString("documentName"),
                        messages = messages,
                        updatedAt = item.optLong("updatedAt")
                    )
                )
            }

            result.sortedByDescending { it.updatedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, chat: SavedChat) {
        val chats = getAll(context).toMutableList()
        val index = chats.indexOfFirst { it.id == chat.id }

        if (index >= 0) {
            chats[index] = chat
        } else {
            chats.add(chat)
        }

        write(context, chats)
    }

    fun delete(context: Context, id: String) {
        write(context, getAll(context).filterNot { it.id == id })
    }

    private fun write(context: Context, chats: List<SavedChat>) {
        val array = JSONArray()

        chats.forEach { chat ->
            val messages = JSONArray()

            chat.messages.forEach { message ->
                val sources = JSONArray()

                message.sources.forEach { source ->
                    sources.put(
                        JSONObject().apply {
                            put("page", source.page)
                            put("excerpt", source.excerpt)
                        }
                    )
                }

                messages.put(
                    JSONObject().apply {
                        put("text", message.text)
                        put("isUser", message.isUser)
                        put("sources", sources)
                    }
                )
            }

            array.put(
                JSONObject().apply {
                    put("id", chat.id)
                    put("title", chat.title)
                    put("documentId", chat.documentId)
                    put("documentName", chat.documentName)
                    put("messages", messages)
                    put("updatedAt", chat.updatedAt)
                }
            )
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
