document.addEventListener('DOMContentLoaded', function(){

  document.getElementById("viewArchiveBtn").addEventListener("click", function(){
          document.getElementById("newMessages").style.display = "none";
          document.getElementById("viewArchiveBtn").style.display = "none";
          document.getElementById("viewInboxBtn").style.display = "block";
          document.getElementById("archivedMessages").style.display = "block";
  });

  document.getElementById("viewInboxBtn").addEventListener("click", function(){
      document.getElementById("archivedMessages").style.display = "none";
      document.getElementById("viewInboxBtn").style.display = "none";
      document.getElementById("newMessages").style.display = "block";
      document.getElementById("viewArchiveBtn").style.display = "block";
  });

});