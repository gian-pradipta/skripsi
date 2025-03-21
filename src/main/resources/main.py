import cv2
import numpy as np

img = cv2.imread("224_normal_2.jpg")

psf = np.zeros((50, 50, 3))
psf = cv2.ellipse(psf, 
                  (25, 25), # center
                  (22, 0), # axes -- 22 for blur length, 0 for thin PSF 
                  15, # angle of motion in degrees
                  0, 360, # ful ellipse, not an arc
                  (1, 1, 1), # white color
                  thickness=-1) # filled

psf /= psf[:,:,0].sum() # normalize by sum of one channel 
                        # since channels are processed independently

imfilt = cv2.filter2D(img, -1, psf)

cv2.imwrite("224_blurr_3.jpg", imfilt)