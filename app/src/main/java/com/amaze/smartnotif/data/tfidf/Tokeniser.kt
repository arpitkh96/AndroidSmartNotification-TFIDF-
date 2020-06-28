package com.amaze.smartnotif.data.tfidf


val stopWords= mapOf<String,Boolean>("i" to true,"me" to true,"my" to true,"myself" to true,"we" to true,"our" to true,"ours" to true,"ourselves" to true,"you" to true, "you're" to true, "you've" to true, "you'll" to true, "you'd" to true,"your" to true,"yours" to true,"yourself" to true,"yourselves" to true,"he" to true,"him" to true,"his" to true,"himself" to true,"she" to true, "she's" to true,"her" to true,"hers" to true,"herself" to true,"it" to true, "it's" to true,"its" to true,"itself" to true,"they" to true,"them" to true,"their" to true,"theirs" to true,"themselves" to true,"what" to true,"which" to true,"who" to true,"whom" to true,"this" to true,"that" to true, "that'll" to true,"these" to true,"those" to true,"am" to true,"is" to true,"are" to true,"was" to true,"were" to true,"be" to true,"been" to true,"being" to true,"have" to true,"has" to true,"had" to true,"having" to true,"do" to true,"does" to true,"did" to true,"doing" to true,"a" to true,"an" to true,"the" to true,"and" to true,"but" to true,"if" to true,"or" to true,"because" to true,"as" to true,"until" to true,"while" to true,"of" to true,"at" to true,"by" to true,"for" to true,"with" to true,"about" to true,"against" to true,"between" to true,"into" to true,"through" to true,"during" to true,"before" to true,"after" to true,"above" to true,"below" to true,"to" to true,"from" to true,"up" to true,"down" to true,"in" to true,"out" to true,"on" to true,"off" to true,"over" to true,"under" to true,"again" to true,"further" to true,"then" to true,"once" to true,"here" to true,"there" to true,"when" to true,"where" to true,"why" to true,"how" to true,"all" to true,"any" to true,"both" to true,"each" to true,"few" to true,"more" to true,"most" to true,"other" to true,"some" to true,"such" to true,"no" to true,"nor" to true,"not" to true,"only" to true,"own" to true,"same" to true,"so" to true,"than" to true,"too" to true,"very" to true,"s" to true,"t" to true,"can" to true,"will" to true,"just" to true,"don" to true, "don't" to true,"should" to true, "should've" to true,"now" to true,"d" to true,"ll" to true,"m" to true,"o" to true,"re" to true,"ve" to true,"y" to true,"ain" to true,"aren" to true, "aren't" to true,"couldn" to true, "couldn't" to true,"didn" to true, "didn't" to true,"doesn" to true, "doesn't" to true,"hadn" to true, "hadn't" to true,"hasn" to true, "hasn't" to true,"haven" to true, "haven't" to true,"isn" to true, "isn't" to true,"ma" to true,"mightn" to true, "mightn't" to true,"mustn" to true, "mustn't" to true,"needn" to true, "needn't" to true,"shan" to true, "shan't" to true,"shouldn" to true, "shouldn't" to true,"wasn" to true, "wasn't" to true,"weren" to true, "weren't" to true,"won" to true, "won't" to true,"wouldn" to true, "wouldn't" to true, "it'll" to true)

suspend fun tokenise(string: String):ArrayList<String>{
    //email
    var text=string.replace(Regex("((http[s]?:\\/\\/(www\\.)?|ftp:\\/\\/(www\\.)?|www\\.){1}([0-9A-Za-z-\\.@:%_\\+~#=]+)+((\\.[a-zA-Z]{2,3})+)(/(.)*)?(\\?(.)*)?)"),"[url]")
    text= text.replace(Regex("([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)"),"[email]");
    //col=cleanStopWords(col)
    text=cleanEmoji(text," [emoji] ")
    text=text.replace(Regex("(($|₹|£)[0-9][0-9,\\.]*)")," [currency] ")
    text=text.replace(Regex("(-?[0-9]+\\s{0,2}°)")," [temperature] ")
    text= text.replace(Regex("(-?[0-9]+\\s{0,2}%)")," [percentage] ")
    text=text.replace(Regex("((?<!([a-zA-z][-_][\\d]?))[0-9][0-9,\\.]*(?!(%|°|\\d)))")," [number] ")
    text=text.replace(Regex("(#[a-zA-z0-9]*)")," [hash] ")
    text=text.replace("\\s+", " ")
    val builder=StringBuilder()
    for (wordUntrimmed in text.split(" ")){
        val word=wordUntrimmed.trim()
        if (stopWords.containsKey(word))continue
        if (!word.startsWithAnyOfThese("[emoji]","[number]","[email]","[url]","[temperature]","[percentage]","[currency]","[hash]"))
            builder.append(cleanAlphaNumeric(word))
        else builder.append(word)
        builder.append(" ")
    }

    val token=ArrayList<String>()
    builder.toString().split(" ").forEach {
        if (it.trim().length>0)
            token.add(it.trim())
    }
    return token
}
private fun cleanNonAscii(string:String):String{
    return string.replace(Regex("[^\\x00-\\x7F]"), "")
}

private fun cleanEmail(text:String,prefix: String,suffix:String):String{
    return text.replace(Regex("([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)"),"$prefix$1$suffix");
}
private fun cleanEmoji(string:String,replace:String):String{
    val out= java.lang.StringBuilder()
    var temp: java.lang.StringBuilder?=null
    for(c in string){
        if (c.isSurrogate())
        {
            if (temp==null)
                temp= java.lang.StringBuilder()
            temp.append(c)
        }
        else
        {
            if (temp!=null){
                out.append(replace)
/*
                    out.append(temp.toString())
                    out.append(suffix)
*/
                temp=null
            }
            out.append(c)
        }
    }
    return out.toString()
    //return string.replace(Regex("(([\uD800-\uDBFF]|[\u2702-\u27B0]|[\uF680-\uF6C0]|[\u24C2-\uF251])+)"),"$prefix$1$suffix")
}
private fun cleanNumbers(text: String,prefix:String,postfix:String):String{
    //((?![ \-_])-?[0-9][0-9,\.]*(?!(%|°|\d)))
    return text.replace(Regex("((?<!([a-zA-z][-_][\\d]?))[0-9][0-9,\\.]*(?!(%|°|\\d)))"),"$prefix$1$postfix")
}
private fun cleanTemperature(text: String,prefix:String,postfix:String):String{
    return text.replace(Regex("(-?[0-9]+\\s{0,2}°)"),"$prefix$1$postfix")
}
private fun cleanPercentage(text: String,prefix:String,postfix:String):String{
    return text.replace(Regex("(-?[0-9]+\\s{0,2}%)"),"$prefix$1$postfix")
}

private fun cleanCurrency(text: String,prefix:String,postfix:String):String{
    return text.replace(Regex("(($|₹|£)[0-9][0-9,\\.]*)"),"$prefix$1$postfix")
}

private fun cleanHashTags(text: String,prefix:String,postfix:String):String{
    return text.replace(Regex("(#[a-zA-z0-9]*)"),"$prefix$1$postfix")
}

private fun cleanAlphaNumeric(string:String):String{
    return string.replace(Regex("[-/_']"),"")
            .replace(Regex("[^a-zA-Z0-9 ]")," ")
}

fun String.startsWithAnyOfThese(vararg text: String): Boolean{
    for (word in text) {
        if (this.startsWith(word))
            return true
    }
    return false
}
