package ir.tapsell.generator.models



class ImpressionEvent(val requestId:String,
                      val adId:String,
                      val adTitle:String,
                      val advertiserCost:Double,
                      val appId:String,
                      val appTitle:String,
                      val impressionTime:Long)