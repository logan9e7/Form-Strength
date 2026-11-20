package com.logan.formstrength;
import org.json.JSONObject;

/** Pure policy checks; real framework permission/Keystore tests still require Android. */
public final class HealthGeminiConfigurationTest {
 private static void check(boolean value,String label){if(!value)throw new AssertionError(label);}
 private static void rejects(Runnable run,String label){try{run.run();throw new AssertionError(label);}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception{
  check(!HealthConnection.platformSupported(26),"API26 unsupported");
  check(!HealthConnection.platformSupported(33),"API33 unsupported");
  check(HealthConnection.platformSupported(34),"API34 supported boundary");
  check(HealthConnection.platformSupported(36),"API36 supported boundary");
  JSONObject status=HealthConnection.unsupportedStatus();
  check(!status.getBoolean("supported"),"explicit unavailable status");
  for(String key:HealthConnection.KEYS)check(!status.getBoolean(key),"unavailable permission "+key);
  check(!GeminiMealClient.validateSettings("","gemini-test-model",true),"model-only update keeps encrypted key");
  check(GeminiMealClient.validateSettings("abcdefghijklmnopqrst1234","gemini-test-model",false),"first key accepted");
  rejects(()->GeminiMealClient.validateSettings("","gemini-test-model",false),"first setup needs key");
  rejects(()->GeminiMealClient.validateSettings("","../other",true),"model path rejected");
  rejects(()->GeminiMealClient.validateSettings("bad key","gemini-test-model",true),"bad replacement rejected");
  java.util.Base64.Encoder encoder=java.util.Base64.getEncoder();
  String iv=encoder.encodeToString(new byte[12]),minimum=encoder.encodeToString(new byte[36]),maximum=encoder.encodeToString(new byte[216]);
  check(GeminiMealClient.validEncryptedState(minimum,iv),"minimum encrypted key state");
  check(GeminiMealClient.validEncryptedState(maximum,iv),"maximum encrypted key state");
  check(!GeminiMealClient.validEncryptedState(null,iv),"missing ciphertext rejected");
  check(!GeminiMealClient.validEncryptedState("",iv),"empty ciphertext rejected");
  check(!GeminiMealClient.validEncryptedState("not base64!",iv),"corrupt ciphertext rejected");
  check(!GeminiMealClient.validEncryptedState(encoder.encodeToString(new byte[35]),iv),"short ciphertext rejected");
  check(!GeminiMealClient.validEncryptedState(encoder.encodeToString(new byte[217]),iv),"oversized ciphertext rejected");
  check(!GeminiMealClient.validEncryptedState(minimum,encoder.encodeToString(new byte[11])),"wrong IV length rejected");
  check(!GeminiMealClient.validEncryptedState(minimum,null),"missing IV rejected");
  check(!GeminiMealClient.validEncryptedState(minimum,"?"),"corrupt IV rejected");
  System.out.println("Passed SDK boundary/unsupported-state and Gemini model-only configuration and encrypted-state bounds.");
 }
}
