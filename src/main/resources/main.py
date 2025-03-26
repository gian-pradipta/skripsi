import cv2
import numpy as np

# img = cv2.imread("224_normal_nuel.jpg")

# psf = np.zeros((50, 50, 3))
# psf = cv2.ellipse(psf, 
#                   (25, 25), # center
#                   (22, 0), # axes -- 22 for blur length, 0 for thin PSF 
#                   15, # angle of motion in degrees
#                   0, 360, # ful ellipse, not an arc
#                   (1, 1, 1), # white color
#                   thickness=-1) # filled

# psf /= psf[:,:,0].sum() # normalize by sum of one channel 
#                         # since channels are processed independently

# imfilt = cv2.filter2D(img, -1, psf)

# cv2.imwrite("224_blurr_3.jpg", imfilt)

# img = cv2.imread("224_normal_cahaya.jpg")

# img = cv2.GaussianBlur(img, (3, 3), 3/6)
# img = cv2.medianBlur(img, 3)

# img = cv2.Laplacian(img, cv2.CV_64F)
# print(img.var())

import cv2

# Load the image
ori = cv2.imread('224_normal_cahaya.jpg')

# Increase brightness by adding a constant value
# brightened_image = cv2.convertScaleAbs(image, alpha=2, beta=50)  # Adjust beta for brightness
img = cv2.medianBlur(ori, 3)

# Save or display the brightened image
cv2.imwrite('brightened_image.jpg', img)
cv2.imshow('Brightened Image', img)
cv2.imshow('Ori', ori)
cv2.waitKey(0)
cv2.destroyAllWindows()