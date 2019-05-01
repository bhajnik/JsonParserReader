# # Parsing-Json-from-server-in-Android
This code will make an http connection with url and reads the Json into a string and convert the Json string into 
json object ie JSONObject jobject=new JSONObject(json_string) where json_string is read from url and parse the jobject
and prepare the list and show it as a list view ListView.

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import spark.implicits._

// Assuming spark session has been created as spark

val userActivity_DF = Seq(
  ("2018-01-01 11:00:00", "u1"),
  ("2018-01-01 12:00:00", "u1"),
  ("2018-01-01 11:00:00", "u2"),
  ("2018-01-02 11:00:00", "u2"),
  ("2018-01-01 12:15:00", "u1"),
  
).toDF("timestamp", "userid")


//Steps:
//•	Load Data in Hive Table.
//•	Read the data from hive, use spark batch (Scala) to do the computation. 
//•	Save the results in parquet with enriched data.

userActivity_DF.insertInto("userActivity")

userActivity = spark.table("userActivity")


val tmo1: Long = 30 * 60
val tmo2: Long = 2 * 60 * 60

def clickSessList(tmo: Long) = udf{ (uid: String, clickList: Seq[String], tsList: Seq[Long]) =>
  def sid(n: Long) = s"$uid-$n"

  val sessList = tsList.foldLeft( (List[String](), 0L, 0L) ){ case ((ls, j, k), i) =>
    if (i == 0) (sid(k + 1) :: ls, 0L, k + 1) else
       if (j + i < tmo) (sid(k) :: ls, j + i, k) else
         (sid(k + 1) :: ls, 0L, k + 1)
  }._1.reverse

  clickList zip sessList
}

val win_part = Window.partitionBy("user_id").orderBy("timestamp")

val enrich1 = userActivity.
  withColumn("ts_diff", unix_timestamp($"timestamp") - unix_timestamp(
    lag($"timestamp", 1).over(win_part))
  ).
  withColumn("ts_diff", when(row_number.over(win_part) === 1 || $"ts_diff" >= tmo1, 0L).
    otherwise($"ts_diff")
  )
//
  val enrich2 = enrich1.
  groupBy("user_id").agg(
    collect_list($"timestamp").as("click_list"), collect_list($"ts_diff").as("ts_list")
  ).
  withColumn("click_sess_id",
    explode(clickSessList(tmo2)($"user_id", $"click_list", $"ts_list"))
  ).
  select($"user_id", $"click_sess_id._1".as("timestamp"), $"click_sess_id._2".as("sess_id"))

