/* File : tachometer_process.i */
%module tachometer_process

%inline %{
extern void* Tachometer_Create();
extern int32_t Tachometer_Init(void* tacho);
extern int32_t Tachometer_Free(void* tacho);
extern int32_t Tachometer_Config(void* tacho, int32_t estimatedFreq);
extern int32_t Tachometer_Process(void* tacho, int16_t* inAudio, float* resultFreq);
%}