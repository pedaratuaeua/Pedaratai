package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allSessions: Flow<List<SavedChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<SavedChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getSessionById(sessionId: String): SavedChatSession? {
        return chatDao.getSessionById(sessionId)
    }

    suspend fun insertSession(session: SavedChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun insertMessage(message: SavedChatMessage) {
        chatDao.insertMessage(message)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        chatDao.updateSessionTitle(sessionId, title)
    }
}
