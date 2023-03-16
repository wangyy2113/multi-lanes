namespace java com.wangyy.multilanes.demo.finagle.api

service A2Bservice {
  string a2b(1:required string msg);
}

service B2Cservice {
  string b2c(1:required string msg);
}

service C2Dservice {
  string c2d(1:required string msg);
}