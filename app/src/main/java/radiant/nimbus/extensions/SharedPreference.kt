package radiant.nimbus.extensions

import android.content.SharedPreferences
import com.google.gson.Gson

fun SharedPreferences.saveInt(key:String, value:Int){
    this.edit().putInt(key, value).apply()
}

fun SharedPreferences.saveLong(key:String, value:Long){
    this.edit().putLong(key, value).apply()
}

fun SharedPreferences.saveFloat(key:String, value:Float){
    this.edit().putFloat(key, value).apply()
}

fun SharedPreferences.saveString(key:String, value:String){
    this.edit().putString(key, value).apply()
}

fun SharedPreferences.saveModel(key:String, value:Any){
    this.edit().putString(key, Gson().toJson(value)).apply()
}

fun SharedPreferences.saveBool(key:String, value:Boolean){
    this.edit().putBoolean(key, value).apply()
}

fun SharedPreferences.saveStringSet(key:String, value:Set<String>){
    this.edit().putStringSet(key, value).apply()
}

fun SharedPreferences.getSavedInt(key:String, defValue:Int=0):Int{
    return this.getInt(key, defValue)
}

fun SharedPreferences.getSavedLong(key:String, defValue:Long=0):Long{
    return this.getLong(key, defValue)
}

fun SharedPreferences.getSavedFloat(key:String, defValue:Float=0f):Float{
    return this.getFloat(key, defValue)
}

fun SharedPreferences.getSavedString(key:String, defValue:String=""):String{
    return this.getString(key, defValue)?:""
}

fun SharedPreferences.getSavedBool(key:String, defValue:Boolean=false):Boolean{
    return this.getBoolean(key, defValue)
}

fun SharedPreferences.getSavedStringSet(key:String, defValue:Set<String>?=setOf()):Set<String>?{
    return this.getStringSet(key, defValue)
}