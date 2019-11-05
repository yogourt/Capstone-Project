


		this.refactor = function(admin) {

			admin.database().ref("/tips").on("value", function(snapshot) {

				var json = {};

				snapshot.forEach(function(item) {
					if(item.child("comments").numChildren() !== 0) {

						var comments = item.child("comments");
						json[item.key] = {};

						console.log("recipe:" + item.key);
						comments.forEach(function(comment) {
							var key = comment.key;

							var newComment = {};

    						if(comment.child("b").val() !== null) {
    							newComment["authorId"] = comment.child("b").val();
    						}
    						if(comment.child("c").val() !== null) {
    							newComment["message"] = comment.child("c").val();
    						}
    						console.log(newComment);

    						json[item.key][key] = newComment;

						});
					}
				});
					
				return admin.database().ref('/comments/').set(json);

			});
		}



			



				/*
				snapshot.child("/tipList").forEach(function(item) {
					var authorId = item.child('authorId').val();
					var recipeId = item.key;


					if(authorId !== null) {

						if(userRec.has(authorId)) {
							userRec.set(authorId, userRec.get(authorId) + ", " + recipeId );
						} else {
							userRec.set(authorId, recipeId);
						}
					}

					item.forEach(function(userIdSnapshot) {
						//is user id indeed
						if(userIdSnapshot.val() === true) {
							var userId = userIdSnapshot.key;
							if(favs.has(userId)) {
								favs.set(userId, favs.get(userId) + ", " + recipeId);
							} else {
								favs.set(userId, recipeId);
							}
						}
					})
				});

				*/




	