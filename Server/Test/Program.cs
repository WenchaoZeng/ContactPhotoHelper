using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Reflection;
using ContactPhotoHelper;
using fastJSON;

namespace Test
{
    class Program
    {
        static void Main(string[] args2)
        {
            JSON.Instance.UseSerializerExtension = false;

            //
            // Submit photo
            //

            //{
            //    String path = Assembly.GetExecutingAssembly().Location;
            //    path = Path.GetDirectoryName(path);
            //    path = path + "\\test.jpg";
            //    byte[] picBytes = File.ReadAllBytes(path);
            //    String picBase64 = Convert.ToBase64String(picBytes);

            //    Dictionary<String, String> args = new Dictionary<string, string>();
            //    args.Add("key", "13564311086");

            //    String result = name.zwc.Caller.Handlers.ContactPhotoHelper.SubmitPhoto(picBase64, args);
            //    Console.WriteLine(result);
            //}

            //
            // Check Update
            // 

            {
                List<MD5> md5List = new List<MD5>() 
                {
                    //new MD5() { Key = "13564311086", Value = "509a452cd456be3d240fd9e7e4bf5c98"},
                    new MD5() { Key = "135643110862"/*, Value = "509a452cd456be3d240fd9e7e4bf5c98"*/}
                };
                String input = JSON.Instance.ToJSON(md5List);

                String result = name.zwc.Caller.Handlers.ContactPhotoHelper.CheckUpdate(input, null);
                Console.WriteLine(result);
            }

            Console.WriteLine("Done");
            Console.ReadKey();
        }
    }
}
