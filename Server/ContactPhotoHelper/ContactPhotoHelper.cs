/**
 * This library will be hosted in https://code.google.com/p/caller/ project r5.
 */
using System;
using System.Collections.Generic;
using System.Text;
using MyGreatIdea;
using System.Web;
using fastJSON;
using System.Reflection;
using System.IO;
using System.Drawing;
using ContactPhotoHelper;
using System.Data.OleDb;
using System.Collections;

namespace name.zwc.Caller.Handlers
{
    public class ContactPhotoHelper
    {
        static string uploadDir = null;
        static string buildFilePath(string key)
        {
            return uploadDir + "\\" + key + ".jpg";
        }

        static DBHelper db = null;
        static ContactPhotoHelper()
        {
            // 
            // Initialize database connection.
            //
            String dbFilePath = String.Empty;
            if (HttpContext.Current != null) // The code is running in a web server.
            {
                dbFilePath = HttpContext.Current.Server.MapPath("~/App_Data/ContactPhotoHelper.mdb");
                uploadDir = HttpContext.Current.Server.MapPath("~/ContactPhotoHelperFiles");
            }
            else // The code is running locally for test purpose.
            {
                dbFilePath = Assembly.GetExecutingAssembly().Location;
                dbFilePath = Path.GetDirectoryName(dbFilePath);

                uploadDir = dbFilePath + "\\ContactPhotoHelperFiles";
                if (!Directory.Exists(uploadDir))
                {
                    Directory.CreateDirectory(uploadDir);
                }

                dbFilePath += "\\App_Data\\ContactPhotoHelper.mdb";
            }
            db = new DBHelper(dbFilePath);

            //
            // Initialize JSON parser.
            //
            JSON.Instance.UseSerializerExtension = false;
        }

        /// <summary>
        /// Update or insert MD5 value
        /// </summary>
        /// <param name="key"></param>
        /// <returns>MD5 value</returns>
        static String updateOrInsertMD5(String key)
        {
            String filePath = buildFilePath(key);

            String md5Value = null;
            if (File.Exists(filePath))
            {
                md5Value = Helpers.GetMD5HashFromFile(filePath);
            }
            else
            {
                md5Value = "";
            }

            if (db.ExecuteUpdate("update [md5] set [value] = ? where [key] = ?", md5Value, key) <= 0)
            {
                db.ExecuteUpdate("insert into [md5]([key], [value]) values(?, ?)", key, md5Value);
            }

            return md5Value;
        }

        static List<MD5> parseMD5ListFromJSON(string input)
        {
            List<MD5> md5List = new List<MD5>();
            ArrayList arrayList = (ArrayList)JSON.Instance.Parse(input);
            foreach (object item in arrayList)
            {
                Dictionary<string, object> itemDictionary = (Dictionary<string, object>)item;
                md5List.Add(new MD5()
                {
                    Key = itemDictionary["Key"].ToString(),
                    Value = itemDictionary["Value"] == null ? "" : itemDictionary["Value"].ToString()
                });
            }
            return md5List;
        }

        public static String SubmitPhoto(String input, Dictionary<String, String> args)
        {
            string key = args["key"];
            if (String.IsNullOrEmpty(key))
            {
                return "Please set the 'key' parameter.";
            }

            // Save picture
            byte[] picBytes = Convert.FromBase64String(input);
            Image pic = Image.FromStream(new MemoryStream(picBytes));
            pic = Helpers.resizeImage(pic, new Size(100, 100));
            String filePath = buildFilePath(key);
            JPEGCompression.SaveJpeg(filePath, pic, 70);

            updateOrInsertMD5(key);

            return "true";
        }
        
        public static String CheckUpdate(String input, Dictionary<String, String> args)
        {
            List<MD5> md5List = parseMD5ListFromJSON(input);

            //
            // Query md5 list from db
            //
            List<MD5> md5DbList = new List<MD5>();
            
            StringBuilder rangeBuilder = new StringBuilder();
            foreach (MD5 md5 in md5List)
            {
                rangeBuilder.AppendFormat("'{0}',", md5.Key);
            }
            rangeBuilder.Remove(rangeBuilder.Length - 1, 1);

            String sql = String.Format("select [key], [value] from [md5] where [key] in ({0})", rangeBuilder.ToString());
            using (OleDbDataReader reader = db.Execute(sql))
            {
                if (reader.HasRows)
                {
                    while (reader.Read())
                    {
                        md5DbList.Add(new MD5() 
                        { 
                            Key = (string)reader["key"], 
                            Value = (string)reader["value"]
                        });
                    }
                }
            }

            //
            // Compare md5 values and get the different values as the result.
            //
            List<MD5> md5ResultList = new List<MD5>();
            foreach (MD5 md5 in md5List)
            {
                bool found = false;
                foreach (MD5 md5InDb in md5DbList)
                {
                    if (md5.Key == md5InDb.Key)
                    {
                        if (md5.Value != md5InDb.Value)
                        {
                            md5ResultList.Add(md5InDb);
                        }
                        found = true;
                        break;
                    }
                }
                if (found)
                {
                    continue;
                }

                // No existing md5 value in the db, try to calculate one.
                String md5Value = updateOrInsertMD5(md5.Key);
                md5ResultList.Add(new MD5() 
                {
                    Key = md5.Key,
                    Value = md5Value
                });
            }

            return JSON.Instance.ToJSON(md5ResultList);
        }
    }
}
