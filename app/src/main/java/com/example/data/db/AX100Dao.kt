package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AX100Dao {
    // Presets
    @Query("SELECT * FROM patch_presets ORDER BY timestamp DESC")
    fun getAllPresets(): Flow<List<PatchPresetEntity>>

    @Query("SELECT * FROM patch_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): PatchPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PatchPresetEntity): Long

    @Query("DELETE FROM patch_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)

    // MIDI Mappings
    @Query("SELECT * FROM midi_mappings ORDER BY ccNumber ASC")
    fun getAllMidiMappings(): Flow<List<MidiMappingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMidiMapping(mapping: MidiMappingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMidiMappings(mappings: List<MidiMappingEntity>)

    @Update
    suspend fun updateMidiMapping(mapping: MidiMappingEntity)

    @Query("DELETE FROM midi_mappings")
    suspend fun clearMidiMappings()

    // Z-Files
    @Query("SELECT * FROM z_files ORDER BY timestamp DESC")
    fun getAllZFiles(): Flow<List<ZFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZFile(zFile: ZFileEntity): Long

    @Query("DELETE FROM z_files WHERE id = :id")
    suspend fun deleteZFileById(id: Long)
}
